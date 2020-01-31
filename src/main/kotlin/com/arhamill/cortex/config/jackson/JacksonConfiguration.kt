package com.arhamill.cortex.config.jackson

import com.arhamill.cortex.NodeRPCConnection
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.bluebank.braid.corda.serialisation.serializers.AmountDeserializer
import io.bluebank.braid.corda.serialisation.serializers.AmountSerializer
import net.corda.client.jackson.JacksonSupport
import net.corda.core.contracts.Amount
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder


@Configuration
class JacksonConfiguration{

    @Bean
    fun rpcObjectMapper(rpc: NodeRPCConnection) = JacksonSupport.createDefaultMapper(rpc.proxy)
            .addMixIn(Amount::class.java, AmountMixin::class.java)

    @Bean
    fun decoder(rpcObjectMapper: ObjectMapper): Jackson2JsonDecoder =
            Jackson2JsonDecoder(rpcObjectMapper, MediaType.APPLICATION_JSON, MediaType.APPLICATION_STREAM_JSON)

    @JsonDeserialize(using = AmountDeserializer::class)
    @JsonSerialize(using = AmountSerializer::class)
    abstract class AmountMixin

}