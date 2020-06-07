# Sacred Geometry
So against all advice and reason, your GM let you take 
[Sacred Geometry](https://www.d20pfsrd.com/feats/general-feats/sacred-geometry/).
Assuming the excessive power and virtually guaranteed success rate is kosher at your table, the
least you can do is not bog the game down for your fellow players while you do math.

This is a calculator (usable as either a command line application or a discord bot) that takes in 
two arguments. The first is either a string of dice rolls, ranging from 1 to 8; OR an expression of 
the form Xd6 or Xd8, where X is between 2 and 20 inclusive. The second is the modified level of the 
spell, from 1 to 9. From there, the calculator will roll the dice for you if necessary, find an 
equation for one of your prime constants, and print it out for you, formatted prettily and with 
parentheses.  
 
This calculator is exhaustive and 100%* accurate - a solution will be found if one exists. It's also
extremely performant, taking well under a second on a moderately-specced machine for the worst 
cases it will encounter (say, rolling 20 1's for a 9th-level spell). In testing, I've successfully 
thrown over 1000 dice at it, and it will stack overflow before reaching the point where a solution 
takes more than a second.

*There are technically a couple of cases (involving non-integer division) it won't consider, but I 
have yet to encounter one of these instances where there isn't an alternate solution it can find.

## Command Line
For the command line version, use the manifest under resources/console. As mentioned above, the 
parameters are the roll string (12345678) or expression (Xd6), and the modified spell level.

## Discord Bot
The discord bot version requires slightly more setup. 
* [Follow this link](https://discord.com/developers/applications) to create a discord application.
* Under the 'Bot' tab, generate a bot token.
* Create the file resource/discord/botToken.txt and copy the token into the file.
* Package using the manifest in resources/discord.
* Running the jar will generate an invite link to add the bot to your server.

Once added, the bot will respond whenever the application is running on some machine. The command is
`!sg {roll string (12345678) or expression (Xd6)} {modified spell level}`.