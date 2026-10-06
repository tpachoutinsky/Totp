package modeles;

public record Ipv4(String interfacetype, String nominterfacename,String ip){
    @Override
    public String toString(){
        return nominterfacename;
    }
}


