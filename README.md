# 02285YSoSirius
Repository for holding the code that will constitute our solution to the third assignment of the course AI and Multi agent Systems at DTU

# Notes
- when running the commands described in `SERVER-README.md`, one would have to pass the `-classpath bin` argument to the inner java command (project root is current working directory). Example: `java -jar server.jar -l levels/MAsimple1.lvl -c "java client.RandomWalkClient"` becomes `java -jar server.jar -l levels/MAsimple1.lvl -c "java -classpath bin client.RandomWalkClient"`
