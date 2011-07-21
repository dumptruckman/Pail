/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dumptruckman.pail.task.event;

import org.dumptruckman.pail.Pail;
import org.quartz.*;
import org.dumptruckman.pail.task.Task;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
/**
 *
 * @author dumptruckman
 */
public class EventScheduler {
    public static boolean scheduleEvent(EventModel event, Scheduler scheduler, Pail pail) {
        JobDetail job;
        Trigger trigger;
        job = newJob(Task.class)
                .withIdentity(event.getName())
                .withDescription(event.getName())
                .build();
        job.getJobDataMap().put("Event", event);
        job.getJobDataMap().put("Pail", pail);

        try {
            trigger = newTrigger()
                    .forJob(job)
                    .withSchedule(cronSchedule(event.getCronEx()))
                    .build();
            try {
                scheduler.scheduleJob(job, trigger);
                return true;
            } catch (SchedulerException se) {
                System.out.println("scheduling exception error");
                return false;
            }
        } catch (java.text.ParseException pe) {
            System.out.println("cron parsing error");
            return false;
        }
    }
    public static boolean scheduleImmediateEvent(EventModel event, Scheduler scheduler, Pail pail) {
        JobDetail job;
        Trigger trigger;
        job = newJob(Task.class).build();
        job.getJobDataMap().put("Event", event);
        job.getJobDataMap().put("Pail", pail);

        trigger = newTrigger()
                .forJob(job)
                .startNow()
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
            System.out.println(trigger.getNextFireTime());
            return true;
        } catch (SchedulerException se) {
            System.out.println("scheduling exception error");
            se.printStackTrace();
            return false;
        }
    }
}
