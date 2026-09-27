package ru.netology.multimedia.error

class HttpException(val code: Int) : Exception()
class EmptyBodyException : Exception()