package org.acme

import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.*
import jakarta.websocket.server.PathParam
import jakarta.websocket.server.ServerEndpoint
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@ApplicationScoped
@ServerEndpoint("/start-websocket/{name}")
class StartWebSocket {

    val sessions: ConcurrentHashMap<String, Session> = ConcurrentHashMap()

    @OnOpen
    fun onOpen(session: Session, @PathParam("name") name: String) {
        println("onOpen> $name")
        sessions[name] = session
    }

    @OnClose
    fun onClose(session: Session, @PathParam("name") name: String) {
        println("onClose> $name")
        sessions.remove(name)
    }

    @OnError
    fun onError(session: Session, @PathParam("name") name: String, throwable: Throwable) {
        println("onError> $name: $throwable")
    }

    @OnMessage
    fun onMessage(message: String, @PathParam("name") name: String) {
        println("onMessage> $name: $message")
        sessions.broadcast(">> $name: $message")
    }

    private fun ConcurrentHashMap<String, Session>.broadcast(message: String) {
        this.values.forEach {
            it.asyncRemote.sendObject(message, {result ->
                takeIf { result.exception !== null }?.apply {
                    println(result.exception)
                }
            })
        }
    }
}
