package com.kobe.dinger.DTOs.sync;

import java.util.List;

public class PlayerResponseDTO {
    private List<PlayerDTO> people;

    public List<PlayerDTO> getPeople(){
        return this.people;
    }

    public void setPeople(List<PlayerDTO> people){
        this.people = people;
    }

}
