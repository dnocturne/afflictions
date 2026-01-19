package com.dnocturne.afflictions.locale;

/**
 * Message keys for localization.
 * These correspond to paths in the language YAML files.
 */
public final class MessageKey {

    private MessageKey() {
    }

    // Prefix
    public static final String PREFIX = "prefix";

    // General
    public static final String RELOAD_SUCCESS = "general.reload-success";
    public static final String RELOAD_FAILED = "general.reload-failed";
    public static final String NO_PERMISSION = "general.no-permission";
    public static final String PLAYER_ONLY = "general.player-only";
    public static final String PLAYER_NOT_FOUND = "general.player-not-found";
    public static final String INVALID_AFFLICTION = "general.invalid-affliction";

    // Affliction
    public static final String AFFLICTION_CONTRACTED = "affliction.contracted";
    public static final String AFFLICTION_CURED = "affliction.cured";
    public static final String AFFLICTION_PROGRESSED = "affliction.progressed";
    public static final String AFFLICTION_EXPIRED = "affliction.expired";
    public static final String AFFLICTION_ALREADY_HAS = "affliction.already-has";
    public static final String AFFLICTION_IMMUNE = "affliction.immune";

    // Affliction list
    public static final String AFFLICTION_LIST_HEADER = "affliction.list.header";
    public static final String AFFLICTION_LIST_ENTRY = "affliction.list.entry";
    public static final String AFFLICTION_LIST_NONE = "affliction.list.none";

    // Affliction info
    public static final String AFFLICTION_INFO_HEADER = "affliction.info.header";
    public static final String AFFLICTION_INFO_LEVEL = "affliction.info.level";
    public static final String AFFLICTION_INFO_DURATION = "affliction.info.duration";
    public static final String AFFLICTION_INFO_CONTRACTED = "affliction.info.contracted";

    // Admin
    public static final String ADMIN_GIVE_SUCCESS = "admin.give.success";
    public static final String ADMIN_GIVE_FAILED = "admin.give.failed";
    public static final String ADMIN_REMOVE_SUCCESS = "admin.remove.success";
    public static final String ADMIN_REMOVE_FAILED = "admin.remove.failed";
    public static final String ADMIN_CLEAR_SUCCESS = "admin.clear.success";
    public static final String ADMIN_CLEAR_FAILED = "admin.clear.failed";

    // Vampirism
    public static final String VAMPIRISM_INFECTED = "vampirism.infected";
    public static final String VAMPIRISM_SUN_BURNING = "vampirism.sun-burning";
    public static final String VAMPIRISM_SUN_WARNING = "vampirism.sun-warning";
    public static final String VAMPIRISM_NIGHT_FALLS = "vampirism.night-falls";
    public static final String VAMPIRISM_DAWN_APPROACHES = "vampirism.dawn-approaches";
    public static final String VAMPIRISM_BLOOD_LOW = "vampirism.blood-low";
    public static final String VAMPIRISM_BLOOD_CRITICAL = "vampirism.blood-critical";
    public static final String VAMPIRISM_HUNGER_START = "vampirism.hunger-start";
    public static final String VAMPIRISM_HUNGER_END = "vampirism.hunger-end";
    public static final String VAMPIRISM_FEEDING = "vampirism.feeding";
    public static final String VAMPIRISM_ABILITY_LOCKED = "vampirism.ability-locked";
    public static final String VAMPIRISM_SIRE_SUCCESS = "vampirism.sire-success";
    public static final String VAMPIRISM_SIRE_RECEIVED = "vampirism.sire-received";

    // Blood commands
    public static final String BLOOD_SYSTEM_DISABLED = "blood.system-disabled";
    public static final String BLOOD_NOT_VAMPIRE = "blood.not-vampire";
    public static final String BLOOD_SET_SUCCESS = "blood.set-success";
    public static final String BLOOD_ADD_SUCCESS = "blood.add-success";
    public static final String BLOOD_REMOVE_SUCCESS = "blood.remove-success";
    public static final String BLOOD_GET_SUCCESS = "blood.get-success";

    // Werewolf
    public static final String WEREWOLF_INFECTED = "werewolf.infected";
    public static final String WEREWOLF_TRANSFORMATION_START = "werewolf.transformation-start";
    public static final String WEREWOLF_TRANSFORMATION_END = "werewolf.transformation-end";
    public static final String WEREWOLF_FULL_MOON_WARNING = "werewolf.full-moon-warning";
    public static final String WEREWOLF_NEW_MOON_WEAKNESS = "werewolf.new-moon-weakness";

    // Werewolf clans
    public static final String CLAN_JOINED = "werewolf.clan.joined";
    public static final String CLAN_LEFT = "werewolf.clan.left";
    public static final String CLAN_PROMOTED_ALPHA = "werewolf.clan.promoted-alpha";
    public static final String CLAN_DEMOTED = "werewolf.clan.demoted";
    public static final String CLAN_RECRUITED = "werewolf.clan.recruited";
    public static final String CLAN_KICKED = "werewolf.clan.kicked";
    public static final String CLAN_INVITE_RECEIVED = "werewolf.clan.invite-received";
    public static final String CLAN_INVITE_SENT = "werewolf.clan.invite-sent";
    public static final String CLAN_INVITE_EXPIRED = "werewolf.clan.invite-expired";
    public static final String CLAN_ALREADY_IN_CLAN = "werewolf.clan.already-in-clan";
    public static final String CLAN_NOT_IN_CLAN = "werewolf.clan.not-in-clan";
    public static final String CLAN_NOT_ALPHA = "werewolf.clan.not-alpha";
    public static final String CLAN_FULL = "werewolf.clan.clan-full";

    // Moon phases
    public static final String MOON_FULL = "time.moon.full-moon";
    public static final String MOON_WANING_GIBBOUS = "time.moon.waning-gibbous";
    public static final String MOON_THIRD_QUARTER = "time.moon.third-quarter";
    public static final String MOON_WANING_CRESCENT = "time.moon.waning-crescent";
    public static final String MOON_NEW = "time.moon.new-moon";
    public static final String MOON_WAXING_CRESCENT = "time.moon.waxing-crescent";
    public static final String MOON_FIRST_QUARTER = "time.moon.first-quarter";
    public static final String MOON_WAXING_GIBBOUS = "time.moon.waxing-gibbous";

    // Curses
    public static final String CURSE_APPLIED = "curse.applied";
    public static final String CURSE_REMOVED = "curse.removed";
    public static final String CURSE_EFFECT_START = "curse.effect-start";
    public static final String CURSE_EFFECT_END = "curse.effect-end";
}
