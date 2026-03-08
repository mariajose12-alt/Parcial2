package com.pucmm.csti19105488;

import com.pucmm.csti19105488.controller.EventoController;
import com.pucmm.csti19105488.controller.RegistroController;
import com.pucmm.csti19105488.controller.UsuarioController;
import com.pucmm.csti19105488.service.UsuarioService;
import com.pucmm.csti19105488.util.HibernateUtil;
import jakarta.persistence.EntityManagerFactory;
import io.javalin.rendering.template.JavalinThymeleaf;
import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.Javalin;
import org.h2.tools.Server;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Main {

    public static void main(String[] args) throws Exception {

        // Configurar Thymeleaf
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);


        // Arrancar servidor H2
        Server h2Server = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092", "-ifNotExists").start();
        System.out.println("H2 Server corriendo en puerto 9092");

        // Inicializar Hibernate
        EntityManagerFactory emf = HibernateUtil.getEntityManagerFactory();
        System.out.println("Hibernate inicializado correctamente");

        // Crear admin inicial
        UsuarioService usuarioService = new UsuarioService();
        usuarioService.crearAdminInicial();

        // Arrancar Javalin
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(templateEngine));
            config.routes.apiBuilder(() -> {
                new UsuarioController().registrarRutas();
                new EventoController().registrarRutas();
                new RegistroController().registrarRutas();
                get("/", ctx -> ctx.redirect("/login"));
            });
        }).start(7000);

        // Cerrar todo al apagar
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            HibernateUtil.close();
            h2Server.stop();
            System.out.println("Servidor apagado correctamente");
        }));
    }
}