package top.quantic.sentry.config;

import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "sentry", ignoreUnknownFields = false)
public class SentryProperties {

    private final Discord discord = new Discord();
    private final Metrics metrics = new Metrics();

    public Discord getDiscord() {
        return discord;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public static class Discord {

        private List<String> administrators = new ArrayList<>();
        private List<String> defaultPrefixes = Lists.newArrayList("!");
        private Coordinator coordinator = new Coordinator();

        public List<String> getAdministrators() {
            return administrators;
        }

        public void setAdministrators(List<String> administrators) {
            this.administrators = administrators;
        }

        public List<String> getDefaultPrefixes() {
            return defaultPrefixes;
        }

        public void setDefaultPrefixes(List<String> defaultPrefixes) {
            this.defaultPrefixes = defaultPrefixes;
        }

        public Coordinator getCoordinator() {
            return coordinator;
        }

        public void setCoordinator(Coordinator coordinator) {
            this.coordinator = coordinator;
        }

        public static class Coordinator {

            private String webhookUrl;
            private String username;
            private String avatarUrl;

            public String getWebhookUrl() {
                return webhookUrl;
            }

            public void setWebhookUrl(String webhookUrl) {
                this.webhookUrl = webhookUrl;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getAvatarUrl() {
                return avatarUrl;
            }

            public void setAvatarUrl(String avatarUrl) {
                this.avatarUrl = avatarUrl;
            }
        }
    }

    public static class Metrics {

        private final Datadog datadog = new Datadog();

        public Datadog getDatadog() {
            return datadog;
        }

        public static class Datadog {

            private String apiKey;
            private String host;
            private int period = 10;
            private boolean enabled = false;
            private String prefix;
            private List<String> tags = new ArrayList<>();
            private List<String> expansions = new ArrayList<>();
            private boolean defaultInclude = true;
            private List<String> include = new ArrayList<>();
            private List<String> exclude = new ArrayList<>();

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPeriod() {
                return period;
            }

            public void setPeriod(int period) {
                this.period = period;
            }

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getPrefix() {
                return prefix;
            }

            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }

            public List<String> getTags() {
                return tags;
            }

            public void setTags(List<String> tags) {
                this.tags = tags;
            }

            public List<String> getExpansions() {
                return expansions;
            }

            public void setExpansions(List<String> expansions) {
                this.expansions = expansions;
            }

            public boolean isDefaultInclude() {
                return defaultInclude;
            }

            public void setDefaultInclude(boolean defaultInclude) {
                this.defaultInclude = defaultInclude;
            }

            public List<String> getInclude() {
                return include;
            }

            public void setInclude(List<String> include) {
                this.include = include;
            }

            public List<String> getExclude() {
                return exclude;
            }

            public void setExclude(List<String> exclude) {
                this.exclude = exclude;
            }
        }
    }

}
