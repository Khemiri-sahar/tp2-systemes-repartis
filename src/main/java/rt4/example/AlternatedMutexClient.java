package rt4.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import rt4.example.AlternatedMutexGrpc.*;
import rt4.example.AlternatedMutexProto.*;

import java.util.Scanner;

public class AlternatedMutexClient {

    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter process ID (0, 1 or 2): ");
        int processId = scanner.nextInt();

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        AlternatedMutexBlockingStub stub = AlternatedMutexGrpc.newBlockingStub(channel);

        Request request = Request.newBuilder().setProcessId(processId).build();

        // Request access
        Response response = stub.requestAccess(request);
        System.out.println("Response from server: " + response.getMessage());

        // If granted, use the resource then release
        if (response.getMessage().contains("granted")) {
            System.out.println("Process " + processId + " is using the resource...");
            Thread.sleep(2000);

            Response releaseResponse = stub.releaseAccess(
                    Request.newBuilder().setProcessId(processId).build()
            );
            System.out.println(releaseResponse.getMessage());
        }

        channel.shutdown();
    }
}
