package com.superiorslayerevent;

import java.util.HashMap;
import java.util.Map;

public enum SuperiorMonster
{
    CRUSHING_HAND(
            5,
            "Crawling hand",
            "Crushing hand"
    ),

    CHASM_CRAWLER(
            10,
            "Cave crawler",
            "Chasm crawler"
    ),

    SCREAMING_BANSHEE(
            15,
            "Banshee",
            "Screaming banshee"
    ),

    SCREAMING_TWISTED_BANSHEE(
            15,
            "Twisted banshee",
            "Screaming twisted banshee"
    ),

    GIANT_ROCKSLUG(
            20,
            "Rock slug",
            "Giant rockslug"
    ),

    COCKATHRICE(
            25,
            "Cockatrice / Moonlight cockatrice",
            "Cockathrice"
    ),

    FLAMING_PYRELORD(
            30,
            "Pyrefiend",
            "Flaming pyrelord"
    ),

    INFERNAL_PYRELORD(
            30,
            "Pyrelord",
            "Infernal pyrelord"
    ),

    MONSTROUS_BASILISK(
            40,
            "Basilisk",
            "Monstrous basilisk"
    ),

    MALEVOLENT_MAGE(
            45,
            "Infernal mage",
            "Malevolent mage"
    ),

    INSATIABLE_BLOODVELD(
            50,
            "Bloodveld",
            "Insatiable bloodveld"
    ),

    INSATIABLE_MUTATED_BLOODVELD(
            50,
            "Mutated bloodveld",
            "Insatiable mutated bloodveld"
    ),

    DIRE_GRYPHON(
            51,
            "Gryphon",
            "Dire gryphon"
    ),

    VITREOUS_JELLY(
            52,
            "Jelly",
            "Vitreous jelly"
    ),

    VITREOUS_WARPED_JELLY(
            52,
            "Warped jelly",
            "Vitreous warped jelly"
    ),

    VITREOUS_CHILLED_JELLY(
            52,
            "Chilled jelly",
            "Vitreous chilled jelly"
    ),

    SPIKED_TUROTH(
            55,
            "Turoth",
            "Spiked turoth"
    ),

    MUTATED_TERRORBIRD(
            56,
            "Warped terrorbird",
            "Mutated terrorbird"
    ),

    MUTATED_TORTOISE(
            56,
            "Warped tortoise",
            "Mutated tortoise"
    ),

    CAVE_ABOMINATION(
            58,
            "Cave horror",
            "Cave abomination"
    ),

    ABHORRENT_SPECTRE(
            60,
            "Aberrant spectre",
            "Abhorrent spectre"
    ),

    REPUGNANT_SPECTRE(
            60,
            "Deviant spectre",
            "Repugnant spectre"
    ),

    BASILISK_SENTINEL(
            60,
            "Basilisk knight",
            "Basilisk sentinel"
    ),

    SHADOW_WYRM(
            62,
            "Wyrm",
            "Shadow wyrm"
    ),

    MAGMA_STRYKEWYRM(
            62,
            "Lava strykewyrm",
            "Magma strykewyrm"
    ),

    CHOKE_DEVIL(
            65,
            "Dust devil",
            "Choke devil"
    ),

    KING_KURASK(
            70,
            "Kurask",
            "King kurask"
    ),

    BLOOD_STARVED_VENATOR(
            74,
            "Venator",
            "Blood-starved venator"
    ),

    MARBLE_GARGOYLE(
            75,
            "Gargoyle",
            "Marble gargoyle"
    ),

    ANCIENT_CUSTODIAN(
            76,
            "Elder custodian stalker",
            "Ancient custodian"
    ),

    ELDER_AQUANITE(
            78,
            "Aquanite",
            "Elder aquanite"
    ),

    NECHRYARCH(
            80,
            "Nechryael / Greater nechryael",
            "Nechryarch"
    ),

    GUARDIAN_DRAKE(
            84,
            "Drake",
            "Guardian Drake"
    ),

    GREATER_ABYSSAL_DEMON(
            85,
            "Abyssal demon",
            "Greater abyssal demon"
    ),

    NIGHT_BEAST(
            90,
            "Dark beast",
            "Night beast"
    ),

    DREADBORN_ARAXYTE(
            92,
            "Araxyte",
            "Dreadborn Araxyte"
    ),

    NUCLEAR_SMOKE_DEVIL(
            93,
            "Smoke devil",
            "Nuclear smoke devil"
    ),

    COLOSSAL_HYDRA(
            95,
            "Hydra",
            "Colossal hydra"
    );

    private final int slayerLevel;
    private final String normalMonster;
    private final String superiorMonster;

    private static final Map<String, SuperiorMonster> SUPERIOR_LOOKUP =
            new HashMap<>();

    static
    {
        for (SuperiorMonster monster : values())
        {
            SUPERIOR_LOOKUP.put(
                    monster.superiorMonster.toLowerCase().trim(),
                    monster
            );
        }
    }

    SuperiorMonster(
            int slayerLevel,
            String normalMonster,
            String superiorMonster)
    {
        this.slayerLevel = slayerLevel;
        this.normalMonster = normalMonster;
        this.superiorMonster = superiorMonster;
    }

    public int getSlayerLevel()
    {
        return slayerLevel;
    }

    public String getNormalMonster()
    {
        return normalMonster;
    }

    public String getSuperiorMonster()
    {
        return superiorMonster;
    }

    public static SuperiorMonster fromNpcName(String npcName)
    {
        if (npcName == null)
        {
            return null;
        }

        return SUPERIOR_LOOKUP.get(
                npcName.toLowerCase().trim()
        );
    }

    public static boolean isSuperior(String npcName)
    {
        return fromNpcName(npcName) != null;
    }
    public int getPoints()
    {
        if (slayerLevel >= 90)
        {
            return 10;
        }
        else if (slayerLevel >= 80)
        {
            return 8;
        }
        else if (slayerLevel >= 70)
        {
            return 6;
        }
        else if (slayerLevel >= 60)
        {
            return 4;
        }
        else if (slayerLevel >= 50)
        {
            return 3;
        }
        else if (slayerLevel >= 30)
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }
}