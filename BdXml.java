package proyectoxmlbd;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmldb.api.base.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.*;

public class BdXml {

    private static Collection col = null;
    private static XMLResource res = null;
    private static String URI = "xmldb:exist://localhost:8080/exist/xmlrpc/db/";
    private static String cole, usuario, pass, xml;

    public BdXml(String usu, String pass, String cole, String xml) {
        BdXml.cole = cole;
        BdXml.pass = pass;
        BdXml.usuario = usu;
        BdXml.xml = xml;
    }

    public BdXml() {
    }

    public static XMLResource conectar() throws Exception {
        String driver = "org.exist.xmldb.DatabaseImpl"; //Driver
        Class cl = Class.forName(driver);//Cargar Driver
        Database database = (Database) cl.newInstance();//Instancia de la BD
        DatabaseManager.registerDatabase(database);//Registrar DB
        database.setProperty("create-database", "true");

        res = null;//lo inicializamos a null        

        col = DatabaseManager.getCollection(URI + cole, usuario, pass);
        col.setProperty("pretty", "true");
        col.setProperty("encoding", "ISO-8859-1");
        res = (XMLResource) col.getResource(xml);

        if (res == null) {
            System.err.println("Resources vacios");
        }

        return res;
    }

    public static String[][] consulta(String consul) throws Exception {
        int i = 0;
        String[][] consulta = null;
        XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
        ResourceSet result = service.query("for $b in doc('/db/" + cole + "/" + xml + "')" + consul + " return $b");
        ResourceIterator it = result.getIterator();
        int tamañoX = Integer.parseInt(String.valueOf(result.getSize()));
        consulta = new String[tamañoX][4];
        while (it.hasMoreResources()) {
            Resource rs = it.nextResource();
            consulta[i] = rs.getContent().toString().replaceAll("\\<.*?>", "").trim().split("\n");
            i++;
        }
        return consulta;
    }
    
    public static String[][] consultaOrder(String consul, String x, int n) throws Exception {
        int i = 0;
        String[][] consulta = null;
        XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
        ResourceSet result=null;
        if(n<1){
            result = service.query("for $b in doc('/db/" + cole + "/" + xml + "')" + consul + " order by $b/"+x+" descending return $b");
        }else{
            result = service.query("for $b in doc('/db/" + cole + "/" + xml + "')" + consul + " order by $b/"+x+" return $b");
        }        
        ResourceIterator it = result.getIterator();
        int tamañoX = Integer.parseInt(String.valueOf(result.getSize()));
        consulta = new String[tamañoX][4];
        while (it.hasMoreResources()) {
            Resource rs = it.nextResource();
            consulta[i] = rs.getContent().toString().replaceAll("\\<.*?>", "").trim().split("\n");
            i++;
        }
        return consulta;
    }

    public static int insertar(String id, String nom, String gen, String price) {
        String[][] consulta = null;
        int esta = 1; //empieza siendo que si esta
        try {
            consulta = consulta("/peliculas/pelicula[id=" + id + "]");
        } catch (Exception ex) {
            System.err.println("Error al consultar id");
        }
        if (consulta.length < 1) { //si esta vacio
            esta = 0;         //entonces no esta e inserta
            XPathQueryService service;
            try {
                service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
                service.query("update insert <pelicula><id>" + id + "</id><nombre>" + nom + "</nombre><genero>"
                        + gen + "</genero><precio>" + price + "</precio></pelicula> into doc('/db/" + cole + "/" + xml + "')/peliculas");
            } catch (XMLDBException ex) {
                System.err.println("Error al insertar");
            }
        }
        return esta;
    }

    public static int borrar(String id) {
        String[][] consulta = null;
        int esta = 0; //empieza siendo que no esta
        try {
            consulta = consulta("/peliculas/pelicula[id=" + id + "]");
        } catch (Exception ex) {
            System.err.println("Error al consultar id");
        }
        if (consulta.length > 0) { //esta lleno
            esta = 1;              //entonces esta y lo borra
            XPathQueryService service;
            try {
                service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
                service.query("update delete doc('/db/" + cole + "/" + xml + "')/peliculas/pelicula[id=" + id + "]");
            } catch (XMLDBException ex) {
                System.err.println("Error al borrar");
            }
        }
        return esta;
    }
    
    public static int modificar(String id, String nom, String gen, String precio){
        String[][] consulta = null;
        int esta = 0; //empieza siendo que no esta
        try {
            consulta = consulta("/peliculas/pelicula[id=" + id + "]");
        } catch (Exception ex) {
            System.err.println("Error al consultar id");
        }
        if (consulta.length > 0) { //esta lleno
            esta = 1;              //entonces esta y lo modifica
        XPathQueryService service;
            try {
                    service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
                    service.query("update replace doc('/"+cole+"/peliculas.xml')/peliculas/pelicula[id='"+id+"'] "
                            + "with <pelicula><id>"+id+"</id><nombre>"+nom+"</nombre><genero>"+gen+"</genero><precio>"+precio+"</precio></pelicula>");
                } catch (XMLDBException ex) {
                    System.err.println("Error al modificar");
                }
            }
        return esta;
    }
}
