package top.quantic.sentry.repository;

import top.quantic.sentry.domain.Setting;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Setting entity.
 */
@SuppressWarnings("unused")
public interface SettingRepository extends MongoRepository<Setting,String> {

    List<Setting> findByGuildAndKey(String guild, String key);

    List<Setting> findByKey(String key);

    List<Setting> findByKeyStartingWith(String key);

    List<Setting> findByGuildAndKeyStartingWith(String guild, String key);

    List<Setting> findByGuild(String guild);

    List<Setting> findByGuildAndKeyAndValue(String guild, String key, String value);
}
