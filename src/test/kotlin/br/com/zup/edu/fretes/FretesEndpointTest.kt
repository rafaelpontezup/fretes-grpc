package br.com.zup.edu.fretes

import br.com.zup.edu.FreteRequest
import br.com.zup.edu.FretesGrpcServiceGrpc.FretesGrpcServiceBlockingStub
import br.com.zup.edu.FretesGrpcServiceGrpc.newBlockingStub
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import javax.inject.Inject

@MicronautTest(transactional = false)
class FretesEndpointTest(
    @Inject val repository: FreteRepository,
    @Inject val grpcClient: FretesGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve calcular frete para um CEP sem mascara`() {

        // cenário
        repository.save(Frete("60325010", BigDecimal("57.91")))
        repository.save(Frete("61760000", BigDecimal("21.89")))
        repository.save(Frete("60711180", BigDecimal("138.14")))

        // ação
        val response = grpcClient.calcula(
            FreteRequest.newBuilder().setCep("61760000").build()
        )

        // validação
        with(response) {
            assertEquals("61760000", cep)
            assertEquals(21.89, valor)
        }
    }

    @Test
    fun `deve calcular frete para um CEP com mascara`() {

        // cenário
        repository.save(Frete("60325010", BigDecimal("57.91")))
        repository.save(Frete("61760000", BigDecimal("21.89")))
        repository.save(Frete("60711180", BigDecimal("138.14")))

        // ação
        val response = grpcClient.calcula(
            FreteRequest.newBuilder().setCep("61.760-000").build()
        )

        // validação
        with(response) {
            assertEquals("61760000", cep)
            assertEquals(21.89, valor)
        }
    }

    @Test
    fun `nao deve calcular CEP quando numero nao informado`() {

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.calcula(
                FreteRequest.newBuilder().build()
            )
        }

        // validação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("CEP não informado", status.description)
        }
    }

    @Test
    fun `nao deve calcular CEP quando numero nao encontrado`() {

        // cenário
        repository.save(Frete("60325010", BigDecimal("57.91")))

        // ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.calcula(
                FreteRequest.newBuilder().setCep("99887000").build()
            )
        }

        // validação
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("CEP inválido ou não encontrado", status.description)
        }
    }

    @Factory
    class GrpcClients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): FretesGrpcServiceBlockingStub {
            return newBlockingStub(channel)
        }
    }

}