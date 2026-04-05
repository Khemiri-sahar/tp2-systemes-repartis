package rt4.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import rt4.example.AlternatedMutexGrpc.*;
import rt4.example.AlternatedMutexProto.*;

import java.util.concurrent.atomic.AtomicInteger;

public class AlternatedMutexServer extends AlternatedMutexImplBase {

    private final AtomicInteger currentTurn = new AtomicInteger(0);

    @Override
    public void requestAccess(Request request, StreamObserver<Response> responseObserver) {
        int processId = request.getProcessId();
        System.out.println("Request of Process " + processId + "...received");

        String msg;
        if (processId == currentTurn.get()) {
            msg = "Access granted...";
            System.out.println("--> Process " + processId + "...granted");
        } else {
            msg = "Access denied...";
            System.out.println("--> Process " + processId + "...denied");
        }

        Response response = Response.newBuilder().setMessage(msg).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void releaseAccess(Request request, StreamObserver<Response> responseObserver) {
        int processId = request.getProcessId();
        currentTurn.set((currentTurn.get() + 1) % 3);

        String msg = "Process " + processId + " released access. Turn passed to process " + currentTurn.get() + ".";
        System.out.println(msg);

        Response response = Response.newBuilder().setMessage(msg).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public static void main(String[] args) throws Exception {
        AlternatedMutexServer serverImpl = new AlternatedMutexServer();
        Server server = ServerBuilder.forPort(9090)
                .addService(serverImpl)
                .build();

        System.out.println("Server started at 0.0.0.0:9090");
        server.start();
        server.awaitTermination();
    }
}
