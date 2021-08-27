package br.com.zup.edu.fretes

import br.com.zup.edu.FreteRequest
import br.com.zup.edu.FreteResponse
import br.com.zup.edu.FretesGrpcServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FretesEndpoint(@Inject val repository: FreteRepository) : FretesGrpcServiceGrpc.FretesGrpcServiceImplBase() {

    override fun calcula(request: FreteRequest, responseObserver: StreamObserver<FreteResponse>) {

        if (request.cep.isNullOrBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("CEP não informado")
                        .asRuntimeException())
            return
        }

        val frete = repository.findByCep(request.cep.replace("[^0-9]".toRegex(), ""))
        if (frete == null) {
            responseObserver.onError(Status.FAILED_PRECONDITION
                        .withDescription("CEP inválido ou não encontrado")
                        .asRuntimeException())
            return
        }

        with(responseObserver) {
            onNext(FreteResponse.newBuilder()
                .setCep(frete.cep)
                .setValor(frete.valor.toDouble())
                .build())
            onCompleted()
        }
    }
}