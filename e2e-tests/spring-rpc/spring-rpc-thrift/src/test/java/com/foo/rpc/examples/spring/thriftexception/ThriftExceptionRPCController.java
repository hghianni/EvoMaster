package com.foo.rpc.examples.spring.thriftexception;

import com.foo.rpc.examples.spring.SpringController;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RPCProblem;

import java.util.HashMap;

public class ThriftExceptionRPCController extends SpringController {

    private ThriftExceptionService.Client client;

    public ThriftExceptionRPCController(){
        super(ThriftExceptionApp.class);
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RPCProblem(new HashMap<String, Object>() {{
            put(ThriftExceptionService.Iface.class.getName(), client);
        }});
    }

    @Override
    public String startClient() {
        String url = "http://localhost:"+getSutPort()+"/thriftexception";
        try {
            // init client
            TTransport transport = new THttpClient(url);
            TProtocol protocol = new TBinaryProtocol(transport);
            client = new ThriftExceptionService.Client(protocol);
        } catch (TTransportException e) {
            e.printStackTrace();
        }

        return url;
    }
}