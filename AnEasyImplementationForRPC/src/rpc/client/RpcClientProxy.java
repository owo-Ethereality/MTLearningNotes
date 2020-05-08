package rpc.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RpcClientProxy<T> implements InvocationHandler {

    private Class<T> serviceInterface;
    private InetSocketAddress addr;

    public RpcClientProxy(Class<T> serviceInterface, String ip, String port) {
        this.serviceInterface = serviceInterface;
        this.addr = new InetSocketAddress(ip, Integer.parseInt(port));
    }

    public T getClientInstance() {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, this);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {

        Socket socket = null;
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try {
            // 创建socket，三次握手连接
            socket = new Socket();
            socket.connect(addr);

            // 转码RPC对应的接口类、方法名、参数列表
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeUTF(serviceInterface.getName());
            oos.writeUTF(method.getName());
            oos.writeObject(method.getParameterTypes());
            oos.writeObject(args);

            // 同步阻塞，等待应答
            ois = new ObjectInputStream(socket.getInputStream());
            return ois.readObject();
        } finally {
            if (socket != null) socket.close();
            if (ois != null) ois.close();
            if (oos != null) oos.close();
        }
    }

    public static void main(String[] args) {
        RpcClientProxy client = new RpcClientProxy<>(IHello.class,"localhost","6666");
        IHello hello = (IHello) client.getClientInstance ();
        System.out.println (hello.sayHello ("test finished successfully"));
    }
}
