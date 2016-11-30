package com.github.yoojia.web

import java.io.File
import java.io.FileInputStream
import java.nio.channels.Channels
import java.nio.file.Path
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
internal class TransferAdapter {

    private val inputFilter: File
    private val lastModified: Long

    constructor(path: Path) {
        inputFilter = path.toFile()
        lastModified = inputFilter.lastModified()
    }

    fun dispatch(request: Request, response: Response) {
        val serverDate = Date(lastModified).toString()
        val clientDate = request.header("If-Modified-Since")
        if(serverDate.equals(clientDate)) {
            response.status(StatusCode.NOT_MODIFIED)
        }else{
            response.header("Last-Modified", serverDate)
            val file = FileInputStream(inputFilter).channel
            val out = Channels.newChannel(response.servletResponse.outputStream)
            file.transferTo(0, file.size(), out)
        }
    }

}