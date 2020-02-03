package com.arhamill.cortex.handlers

import com.arhamill.cortex.NodeRPCConnection
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.sumTokenStatesOrZero
import net.corda.core.contracts.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import rx.RxReactiveStreams.toPublisher
import rx.Single

@Component
class CortexHandler(rpc: NodeRPCConnection) {

    private val proxy = rpc.proxy
    private val classloader = rpc.cordappClassloader

    fun snapshot(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(String::class.java).flatMap {
        val clazz = Class.forName(it, true, classloader).asSubclass(ContractState::class.java)
        ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(toPublisher(Single.just(proxy.vaultQuery(clazz).states.groupStates())),
                        ParameterizedTypeReference.forType(Map::class.java))
    }

    fun updates(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(String::class.java).flatMap {
        val clazz = Class.forName(it, true, classloader).asSubclass(ContractState::class.java)
        ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(toPublisher(
                        proxy.vaultTrackByCriteria(clazz, QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL)).updates.map { update ->
                            ContractStateUpdate(
                                    consumed = update.consumed.groupStates(),
                                    produced = update.produced.groupStates()
                            )
                        }),
                        ParameterizedTypeReference.forType(ContractStateUpdate::class.java))
    }

    fun linearSnapshot(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(String::class.java).flatMap {
        val clazz = Class.forName(it, true, classloader).asSubclass(LinearState::class.java)
        ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(toPublisher(Single.just(proxy.vaultQuery(clazz).states.groupLinearStates())),
                        ParameterizedTypeReference.forType(Map::class.java))
    }

    fun linearUpdates(request: ServerRequest): Mono<ServerResponse> = request.bodyToMono(String::class.java).flatMap {
        val clazz = Class.forName(it, true, classloader).asSubclass(LinearState::class.java)
        ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(toPublisher(
                        proxy.vaultTrackByCriteria(clazz, QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL)).updates.map { update ->
                            LinearStateUpdate(
                                    consumed = update.consumed.groupLinearStates(),
                                    produced = update.produced.groupLinearStates()
                            )
                        }),
                        ParameterizedTypeReference.forType(LinearStateUpdate::class.java))
    }

    fun tokenSnapshot(request: ServerRequest): Mono<ServerResponse> = ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(toPublisher(Single.just(proxy.vaultQuery(FungibleToken::class.java).states.sumTokens())),
                        ParameterizedTypeReference.forType(Map::class.java))

    fun tokenUpdates(request: ServerRequest): Mono<ServerResponse> = ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(
                    toPublisher(proxy.vaultTrackByCriteria(FungibleToken::class.java, QueryCriteria.VaultQueryCriteria(status = Vault.StateStatus.ALL)).updates.map {
                        TokenUpdate(
                                consumed = it.consumed.sumTokens(),
                                produced = it.produced.sumTokens()
                        )
                    }), ParameterizedTypeReference.forType(TokenUpdate::class.java)
            )

    fun Collection<StateAndRef<FungibleToken>>.sumTokens() = map { it.state.data }
            .groupBy { it.issuedTokenType }.mapValues { it.value.sumTokenStatesOrZero(it.key) }

    fun Collection<StateAndRef<LinearState>>.groupLinearStates() = map { it.state.data.linearId to it.state.data }.toMap()

    fun Collection<StateAndRef<ContractState>>.groupStates() = map { it.ref to it.state.data }.toMap()
}

class TokenUpdate(val consumed: Map<IssuedTokenType, Amount<IssuedTokenType>>, val produced: Map<IssuedTokenType, Amount<IssuedTokenType>>)

class LinearStateUpdate(val consumed: Map<UniqueIdentifier, ContractState>, val produced: Map<UniqueIdentifier, ContractState>)

class ContractStateUpdate(val consumed: Map<StateRef, ContractState>, val produced: Map<StateRef, ContractState>)