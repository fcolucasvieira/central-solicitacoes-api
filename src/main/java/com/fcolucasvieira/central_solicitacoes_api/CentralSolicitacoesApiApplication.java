package com.fcolucasvieira.central_solicitacoes_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
public class CentralSolicitacoesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentralSolicitacoesApiApplication.class, args);
	}

}
