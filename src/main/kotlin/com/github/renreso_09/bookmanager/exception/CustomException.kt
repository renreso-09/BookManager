import org.springframework.http.HttpStatus

abstract class HttpException(
    val httpStatus: HttpStatus,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class NotFoundException(message: String) : HttpException(
    httpStatus = HttpStatus.NOT_FOUND,
    message = message
)

class BadRequestException(message: String) : HttpException(
    httpStatus = HttpStatus.BAD_REQUEST,
    message = message
)

class InternalException(message: String) : HttpException(
    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    message = message
)