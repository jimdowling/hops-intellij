package com.logicalclocks.actions;

//import io.hops.cli.action.JobRunAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.logicalclocks.HopsPluginUtils;
import io.hops.cli.config.HopsworksAPIConfig;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HopsRunJob extends AnAction {


    @Override
    public void update(AnActionEvent e) {
        // Set the availability based on whether a project is open
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);

    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        HopsPluginUtils util=new HopsPluginUtils();
        Project proj=e.getProject();
        String hopsworksApiKey = util.getAPIKey(proj);
        String hopsworksUrl = util.getURL(proj);
        String projectName = util.getProjectName(proj);
        String jobName=util.getJobName(proj);
        String destination=util.getDestination(proj);
        String userArgs=util.getUserArgs(proj);
        String localFilePath =e.getDataContext().getData("virtualFile").toString();

        try {

            HopsworksAPIConfig hopsworksAPIConfig = new HopsworksAPIConfig( hopsworksApiKey, hopsworksUrl, projectName);
            //upload app first?
            //FileUploadAction action = new FileUploadAction(hopsworksAPIConfig,destination,localFilePath);
            //action.execute();
            //execute run job
            JobRunAction runJob=new JobRunAction(hopsworksAPIConfig,jobName,userArgs);
            int status=runJob.execute();

            if (status == 200 || status == 201) {
                StringBuilder sb=new StringBuilder(" Job Started: ").append(jobName).append(" | Execution Id:").append(runJob.getExecId());
                PluginNoticifaction.notify(e.getProject(),sb.toString());
            } else PluginNoticifaction.notify(e.getProject()," Job :"+jobName+" | Start Failed");

        } catch (IOException ex) {
            PluginNoticifaction.notify(e.getProject(),ex.getMessage());
            Logger.getLogger(JobRunAction.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }  catch (Exception ex) {
            Logger.getLogger(HopsRunJob.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    public  Notification notify(Project project, String content) {
        NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Hopsworks Plugin", NotificationDisplayType.BALLOON, true);
        final Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
        notification.notify(project);
        return notification;
    }
}
