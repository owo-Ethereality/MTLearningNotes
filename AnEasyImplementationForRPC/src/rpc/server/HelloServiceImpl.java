package rpc.server;

import rpc.client.IHello;

public class HelloServiceImpl implements IHello {
    @Override
    public String sayHello(String string) {
        return "Hello, ".concat(string);
    }
}
