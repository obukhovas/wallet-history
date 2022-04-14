package com.ao.wallet.web

import com.ao.wallet.InvalidHistoryRangeException
import com.ao.wallet.dto.ErrorMessage
import com.ao.wallet.dto.ErrorsResponse
import com.ao.wallet.enum.Error
import com.ao.wallet.enum.Error.INVALID_FIELD
import com.ao.wallet.enum.Error.JSON_PARSE
import com.ao.wallet.enum.Error.MISSING_FIELD
import com.ao.wallet.enum.Error.UNEXPECTED
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.Locale
import javax.validation.ConstraintViolationException


@RestControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

    private companion object : KLogging()

    @Autowired
    private lateinit var messageSource: MessageSource

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.warn(ex.message, ex)
        val errorMessages = ex.bindingResult.fieldErrors.map {
            getMessage(INVALID_FIELD, it.field, it.defaultMessage)
        }
        return ResponseEntity.badRequest().body(ErrorsResponse(errorMessages))
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> {
        logger.warn(ex.message, ex)
        val cause = ex.cause
        val response = if (cause is MissingKotlinParameterException) {
            handle(cause)
        } else {
            ErrorsResponse(getMessage(JSON_PARSE))
        }
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    fun handle(ex: Exception): ErrorsResponse {
        logger.error(ex.message, ex)
        return ErrorsResponse(getMessage(UNEXPECTED))
    }

    @ExceptionHandler(value = [MissingKotlinParameterException::class])
    @ResponseStatus(BAD_REQUEST)
    fun handle(ex: MissingKotlinParameterException): ErrorsResponse {
        logger.warn(ex.message, ex)
        return ErrorsResponse(getMessage(MISSING_FIELD, ex.parameter.name))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(BAD_REQUEST)
    fun handle(ex: ConstraintViolationException): ErrorsResponse {
        logger.warn(ex.message, ex)
        val errorMessages = ex.constraintViolations.map {
            getMessage(INVALID_FIELD, it.propertyPath.toString(), it.message)
        }
        return ErrorsResponse(errorMessages)
    }

    @ExceptionHandler(InvalidHistoryRangeException::class)
    @ResponseStatus(BAD_REQUEST)
    fun handle(ex: InvalidHistoryRangeException): ErrorsResponse {
        logger.warn(ex.message, ex)
        return ErrorsResponse(ex.message)
    }

    private fun getMessage(error: Error, vararg args: String?): ErrorMessage {
        return ErrorMessage(messageSource.getMessage(error.messageCode, args, Locale.getDefault()))
    }
}