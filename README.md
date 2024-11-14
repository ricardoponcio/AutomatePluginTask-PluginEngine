# PluginEngine
## An Automate Plugin Task module

Follow the instruction in the repository [PluginSdk](https://github.com/ricardoponcio/AutomatePluginTask-PluginSdk) to create a plugin project.  
You can visit the page [SampleHelloPlugin](https://github.com/ricardoponcio/AutomatePluginTask-SampleHelloPlugin) to view a plugin example.

This project was created to help me execute long operations with progress callback and post log analysis.

There is an S3 configuration module with a CRUD to create, list and delete S3 configurations. This will be used to persist the plugin files.  
*Obs.: Update is not allowed because it can leak the api secret and key, so you can remove and create it again*

Also, there is a controller to deal with plugins, you should use `persist` to upload the plugin jar file and get the UUID from it. Then use this UUID to retrieve the parameters and ask for sync execution using `load` method, or async execution using `loadAsync`, also must include the parameters in the body to the execution.

> UNDER DEVELOPMENT

Any doubts? Send a message:  
E-mail: **ricardo@poncio.dev**  
Telegram: **@rponcio**
