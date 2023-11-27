package edu.northeastern.cs5500.starterbot.model;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnegative;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BattleRecord {

    @Builder.Default boolean trainerWins = false;
    @Builder.Default @Nonnegative Integer coinsGained = 0; // for trainer
    @Builder.Default @Nonnegative Integer expGained = 0; // for pokemon

    @Builder.Default List<String> battleRounds = new ArrayList<>();
}
