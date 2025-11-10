package cr.ac.una.agenda.controller;

import cr.ac.una.agenda.service.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AgendaController {

    @Autowired
    AgendaService  agendaService;


    @GetMapping("/prolog")
    public Integer prolog(){
        return agendaService.sum(1,19);

    }
}
