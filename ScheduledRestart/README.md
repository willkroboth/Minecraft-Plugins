<p>This plugin allows for a server admin to setup times when thier server will automatically restart. It has options for warning users at minute intervals before the restart, as well as waiting for players to leave the server before restarting.</p>

<p>Here is the default config file:</p>
<p>
restarts:

  \- {time: "00:00", waitForPlayers: "false", warnPlayers: "true", warnIntervals: "30, 15, 10, 5, 2, 1"}
	
  \- {time: "12:00", waitForPlayers: "true"}
</p>

<p>All the restarts must go in a list in the restarts section to be found by the plugin. All arguments must go in " " in order for the plugin to understand them properly. Here is each argument explained:</p>

<p>time: A time in a 24-hour format, with the hour and minute separated by a ':', that defines the time of day when the server will restart. This argument is required, and if it is missing or formatted improperly, the plugin will skip the entry.</p>

<p>waitForPlayers: If this argument is "true", the plugin will only restart once there are no players on the server. If it is "false", missing, or anything else, the server will restart at the given time. In the console logs, a restart that waits is called "soft", while a restart that does not wait is called "hard".</p>

<p>warnPlayers: If this argument is "true", the plugin will broadcast chat messages to players realted to how the server is restarting. If the restart is soft, the plugin will tell the players "Server will automatically restart once all players leave" at the restart time. If the restart is hard, the plugin will tell the players "Server is automatically restarting" before restarting soon after. If it is "false", missing, or anything else, the server will not send any messages to the players.</p>

<p>warnIntervals: This argument only applies if waitForPlayers=false and warnPlayers=true. It should be a string of integers separated by ',' that represent minutes before the restart when the server should tell the players "Server will automatically restart in " + interval + " minutes". If the argument is missing or empty, the plugin will skip over it and use no intervals will be used. If there are any numbers that can not be read as integers, the plugin will announce that in red and not use any intervals for that restart until the problem is fixed.</p>

<p>Note: In order for your server to restart again after this plugin shuts it down, you need to setup something external (since the server is no longer running, plugins can not do anything). You should be able to find a solution for your device and computer online, but here's what I do for my paper server running on a Windows computer:</p>
<ol>
	<li>Create a new text file in notepad and paste the command you use to start your server into it (Something like "java -Xms3G -Xmx3G paper.jar -nogui")</li>
	<li>Go File>Save As... and give your file any name followed by ".cmd". This will create a command file that will start your server when run. (You can also double click the file to start the server)</li>
	<li>Open your spigot.yml, and you should find the two options below under "settings".</li>
	restart-on-crash: true
  restart-script: ./start.sh
	<li>Change "./start.sh" to whatever you named your file (including the .cmd), and you're done!</li>
</ol>
<p>Now, when this plugin restarts your server, or you use the /restart command, the server will automatically start again. (If you don't want the server to start again, use /stop)</p>
