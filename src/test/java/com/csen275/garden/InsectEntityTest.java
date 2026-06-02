package com.csen275.garden;

import com.csen275.garden.domain.insect.Insect;
import com.csen275.garden.domain.insect.Parasite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InsectEntityTest {

    @Test
    void parasiteIsAnInsectWithDamage() {
        Insect insect = new Parasite("aphid", 15);
        Parasite parasite = (Parasite) insect;

        assertEquals("aphid", parasite.getName());
        assertEquals(15, parasite.getDamage());
    }
}
