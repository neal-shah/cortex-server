package com.arhamill.cortex.routes

import com.arhamill.cortex.handlers.CortexHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class CortexRouter {
    @Bean
    fun routes(handler: CortexHandler): RouterFunction<ServerResponse> = router {
        ("/me").nest {
            GET("/", handler::whoAmI)
        }
        ("/states").nest {
            accept(MediaType.APPLICATION_JSON).nest { POST("/", handler::snapshot) }
            accept(MediaType.TEXT_EVENT_STREAM).nest { POST("/updates", handler::updates) }
        }
        ("/linear").nest {
            accept(MediaType.APPLICATION_JSON).nest { POST("/", handler::linearSnapshot) }
            accept(MediaType.TEXT_EVENT_STREAM).nest { POST("/updates", handler::linearUpdates) }
        }
        ("/tokens").nest {
            accept(MediaType.APPLICATION_JSON).nest { POST("/", handler::tokenSnapshot) }
            accept(MediaType.TEXT_EVENT_STREAM).nest { POST("/updates", handler::tokenUpdates) }
        }
    }
}