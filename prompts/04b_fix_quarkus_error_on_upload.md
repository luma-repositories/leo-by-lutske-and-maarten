# Prompt
Fix the following error on recipe upload:

2026-03-01 16:00:20,600 ERROR [io.qua.dep.dev.IsolatedDevModeMain] (vert.x-worker-thread-1) Failed to start quarkus: io.quarkus.dev.appstate.ApplicationStartException: java.lang.RuntimeException: Failed to start quarkus
at io.quarkus.dev.appstate.ApplicationStateNotification.waitForApplicationStart(ApplicationStateNotification.java:58)
at io.quarkus.runner.bootstrap.StartupActionImpl.runMainClass(StartupActionImpl.java:123)
at io.quarkus.deployment.dev.IsolatedDevModeMain.restartApp(IsolatedDevModeMain.java:222)                              
at io.quarkus.deployment.dev.IsolatedDevModeMain.restartCallback(IsolatedDevModeMain.java:203)                         
at io.quarkus.deployment.dev.RuntimeUpdatesProcessor.doScan(RuntimeUpdatesProcessor.java:537)                          
at io.quarkus.deployment.dev.RuntimeUpdatesProcessor.doScan(RuntimeUpdatesProcessor.java:437)                          
at io.quarkus.vertx.http.runtime.devmode.VertxHttpHotReplacementSetup$4.handle(VertxHttpHotReplacementSetup.java:152)  
at io.quarkus.vertx.http.runtime.devmode.VertxHttpHotReplacementSetup$4.handle(VertxHttpHotReplacementSetup.java:139)  
at io.vertx.core.impl.ContextBase.lambda$executeBlocking$0(ContextBase.java:137)                                       
at io.vertx.core.impl.ContextInternal.dispatch(ContextInternal.java:264)                                               
at io.vertx.core.impl.ContextBase.lambda$executeBlocking$1(ContextBase.java:135)                                       
at org.jboss.threads.ContextHandler$1.runWith(ContextHandler.java:18)                                                  
at org.jboss.threads.EnhancedQueueExecutor$Task.run(EnhancedQueueExecutor.java:2449)                                   
at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1452)                             
at org.jboss.threads.DelegatingRunnable.run(DelegatingRunnable.java:29)                                                
at org.jboss.threads.ThreadLocalResettingRunnable.run(ThreadLocalResettingRunnable.java:29)                            
at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)                               
at java.base/java.lang.Thread.run(Thread.java:1583)                                                                    
Caused by: java.lang.RuntimeException: Failed to start quarkus                                                                 
at io.quarkus.runner.ApplicationImpl.doStart(Unknown Source)                                                           
at io.quarkus.runtime.Application.start(Application.java:101)                                                          
at io.quarkus.runtime.ApplicationLifecycleManager.run(ApplicationLifecycleManager.java:110)                            
at io.quarkus.runtime.Quarkus.run(Quarkus.java:70)                                                                     
at io.quarkus.runtime.Quarkus.run(Quarkus.java:43)                                                                     
at io.quarkus.runtime.Quarkus.run(Quarkus.java:123)                                                                    
at io.quarkus.runner.GeneratedMain.main(Unknown Source)                                                                
at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)               
at java.base/java.lang.reflect.Method.invoke(Method.java:580)                                                          
at io.quarkus.runner.bootstrap.StartupActionImpl$1.run(StartupActionImpl.java:104)                                     
... 1 more                                                                                                             
Caused by: javax.enterprise.inject.spi.DeploymentException: io.quarkus.runtime.configuration.ConfigurationException: Failed to load config value of type class java.lang.String for: app.import.ocr.tessdata-pathFailed to load config value of type class java.lang.String for: app.import.ocr.stub-text                                                                                    
at io.quarkus.arc.runtime.ConfigRecorder.validateConfigProperties(ConfigRecorder.java:70)                              
at io.quarkus.deployment.steps.ConfigBuildStep$validateConfigValues1665125174.deploy_0(Unknown Source)                 
at io.quarkus.deployment.steps.ConfigBuildStep$validateConfigValues1665125174.deploy(Unknown Source)                   
... 11 more                                                                                                            
Suppressed: java.util.NoSuchElementException: SRCFG00014: The config property app.import.ocr.tessdata-path is required but it could not be found in any config source                                                                                 
at io.smallrye.config.SmallRyeConfig.convertValue(SmallRyeConfig.java:294)                                     
at io.smallrye.config.inject.ConfigProducerUtil.getValue(ConfigProducerUtil.java:104)                          
at io.quarkus.arc.runtime.ConfigRecorder.validateConfigProperties(ConfigRecorder.java:60)                      
... 13 more                                                                                                    
Suppressed: java.util.NoSuchElementException: SRCFG00014: The config property app.import.ocr.stub-text is required but it could not be found in any config source                                                                                     
at io.smallrye.config.SmallRyeConfig.convertValue(SmallRyeConfig.java:294)                                     
at io.smallrye.config.inject.ConfigProducerUtil.getValue(ConfigProducerUtil.java:104)                          
at io.quarkus.arc.runtime.ConfigRecorder.validateConfigProperties(ConfigRecorder.java:60)                      
... 13 more                                                                                                    
Caused by: io.quarkus.runtime.configuration.ConfigurationException: Failed to load config value of type class java.lang.String for: app.import.ocr.tessdata-pathFailed to load config value of type class java.lang.String for: app.import.ocr.stub-text      
... 14 more      