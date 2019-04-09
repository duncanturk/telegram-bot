# telegram-bot
A bot for the Telegram messenger

# Features
Currently the bot only features a extremely handy and short syntaxed reminder.

## Reminder

The reminder function allows a easy management of notifications using a extremely short and handy syntax, in the way that the bot sends you a message after a given duration or at a given point of time.

The reminder uses the `/r` or `/remind` command.
The general syntax is `/r <delay or time> <content of the message the bot sends you`.

### Reminder after a given delay

The delay is given in the format `/[\d]+[smhdw]/` where `s`, `m`, `h`, `d` or `w`.
Multiple of these can be chained by delimiting using a space.
If multiple delays are given they are summed up.

Examples:

`/r 2d remid me in two days` will send you "remind me in two days" in exactly two days.

`/r 1m 30s text` will send you "text" after 90 seconds.

`/r 1m 20s 5m lalala` will send you "lalala" after 6 minutes and 20 seconds.


Abbreviations: `s` - second, `m` - minute, `h` - hour, `d` - day and `w` for week.

### Remind at a specific time
The bot can also remind you a a specific time and date.
Again this is done using a extra short syntax.

Assumed it is 1PM 23 minutes and 17 seconds on 6th of november 2016.

#### Specify a time
Setting the time is indicated by using a colon, you can think of the first colon in a time expression as the delimiter of hours and minutes.
If either hour or minute should be the same as it is now just don't write it, for example:

`/r 13:45 hey` will send you "hey" at 1PM 45 minutes the same day.

`/r 14: test` will send you "test" at 2PM 23 minutes and 17 seconds the same day.

`/r :30 text` will remind you half past 1PM.

If the given time is in the past, the reminder will be send the next day, so `/r 6: good morning` will send you "good morning" on 6th of november at 6am 23 minutes.

If only the minute is mentioned and it is smaller than the current minute the bot will use the next hour, so `/r :15 ring` will remind you at 2PM 15 minutes.

Seconds can be mentioned after a second colon. If the second should be set, the minute must be set manually:

`/r :30:20 a reminder` will remind you 20 seconds after half past 1PM.


#### Specify a date
Specifying a date works like setting the time but using a dot instead of a colon. For the Date the first part is the day and the second part the month. Setting a year is not supported.

`/r 7. now` will send you "now" tomorrow the same time.

`/r .12 Saint Nicholas Day` will remind you at december the 6th at 1PM 23minutes and 17 seconds.

`/r 4. reminder` will remind you on 4th of december.

---

Both date and time can be given. The order doesn't matter. so `/r .12 :30 test` and `/r :30 12. test` are equivalent and valid.

Delays and time specifications cannot be combined.
