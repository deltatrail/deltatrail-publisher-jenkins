package io.jenkins.plugins.Deltatrail;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.model.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.tasks.BuildStepMonitor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class Publisher extends Notifier {
    private static final Logger logger = Logger.getLogger(Publisher.class.getName());

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String entryTitle;
    private String logText;

    @DataBoundConstructor
    public Publisher() {
        super();
    }


    public Publisher(final String logText, final String entryTitle){
        this.logText = logText;
        this.entryTitle = entryTitle;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;

    }

    public String getLogText() {
        return logText;
    }

    public String getEntryTitle() { return entryTitle; }

    @DataBoundSetter
    public void setEntryTitle(final String entryTitle) { this.entryTitle = entryTitle; }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        DescriptorImpl descriptor = getDescriptor();
        String baseURL = descriptor.getBaseURL();
        String apiToken = descriptor.getAPIToken();

        // PrintStream logger = listener.getLogger();

        String title = !this.entryTitle.isEmpty() ? this.entryTitle : String.format("%s", build.getFullDisplayName());
        EnvVars environment = build.getEnvironment(listener);

        OkHttpClient client = new OkHttpClient();
        JSONObject postBody = new JSONObject();
        postBody.put("title", title);
        postBody.put("body", environment.expand(this.logText));
        postBody.put("tags", new JSONArray());
        postBody.put("token", apiToken);
        RequestBody body = RequestBody.create(JSON, postBody.toString());
        logger.println("Posting " + postBody.toString());

        Request request = new Request.Builder()
                .url(baseURL + "entry")
                .post(body)
                .build();

        client.newCall(request).execute());
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        return true;
    }

    @Extension
    public static final class DescriptorImpl
            extends BuildStepDescriptor<hudson.tasks.Publisher> {

        private String baseURL = "";
        private String apiToken = "";
        private String logText = "";
        private String entryTitle = "";

        public DescriptorImpl() {
            load();
        }

        public String getBaseURL() {
            logger.info("Descript getBASEURL " + baseURL);
            return baseURL;
        }


        public String getAPIToken() {
            return apiToken;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Publish to Deltatrail";
        }

        @Override
        public Publisher newInstance(@CheckForNull StaplerRequest req, @Nonnull JSONObject formData) throws FormException {
            logText = formData.getString("logText");
            entryTitle = formData.getString("logTitle");
            return new Publisher(logText, entryTitle);
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            logger.info("CONFIGURE");
            JSONObject configuration = json.getJSONObject("deltatrailPublisher");

            baseURL = configuration.getString("deltatrailBaseURL");
            baseURL = baseURL.endsWith("/") ? baseURL : baseURL + "/";

            apiToken = configuration.getString("deltatrailAPIToken");

            logger.info("This time got " + baseURL + ", " + apiToken);
            save();
            return super.configure(req, json);
        }
    }
}
