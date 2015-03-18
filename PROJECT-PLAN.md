Use multiagent approach.

Design an agent the is able to solve a single agent level decently. Refine this agent with communication assets and evolve it into a multi agent system.

We need good heuristics, better than Manhatten distance. Do we want to rely on heuristics on do we want to rely more on hierarchical planning?

We probably want to do online planning, since we plan on doing multi-agent. Do we want to do plan merging? Initial thoughts is no.

Thoughts about conflict resolution: The server will only tell us that a single action has failed, not that there is a conflict in an action set. If we want to avoid backtracking
we need to make sure that we detect conflicts in an action set before sending to server.

Some thoughts on milestones/deadlines

31/03/2015: Single agent client finished, and able to solve the levels in the /levels folder in a satisfying fashion.

14/04/2015: AT LEAST have a completely thought out system for agent communication, conflicts resolutions etc. Ideally also implemented.

From here we have three weeks, in which we can improve our solution. Things will probably have to be redone a couple of times, so it is good to have a lot of time.

18/05/2015: submission deadline for contest solutions.

02/06/2015: submission deadline for report.

Homework for next time: Work on the agent. More thoughts on how we solve this problem optimally.