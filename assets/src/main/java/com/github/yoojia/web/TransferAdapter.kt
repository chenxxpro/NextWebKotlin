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

    private val mInputFilter: File
    private val mLastModified: Long

    constructor(path: Path) {
        mInputFilter = path.toFile()
        mLastModified = mInputFilter.lastModified()
    }

    fun dispatch(request: Request, response: Response) {
        val serverDate = Date(mLastModified).toString()
        val clientDate = request.header("If-Modified-Since")
        if(serverDate.equals(clientDate)) {
            response.setStatusCode(StatusCode.NOT_MODIFIED)
        }else{
            response.addHeader("Last-Modified", serverDate)
            val file = FileInputStream(mInputFilter).channel
            val out = Channels.newChannel(response.raw.outputStream)
            file.transferTo(0, file.size(), out)
        }
    }

}