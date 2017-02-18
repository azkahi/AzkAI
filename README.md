# AzkAI
An AI for FightingICE game (http://www.ice.ci.ritsumei.ac.jp/~ftgaic/) which learns through reinforcement learning.
This AI uses a modified Monte Carlo Tree Search algorithm and apply machine learning through SARSA algorithm with a learning rate of 0.5 and discount rate of 0.9.

# Explanation
When the game start, the AI will read the external knowledge base and save it to its temporary data (making it local for more efficient use). With Monte Carlo Tree Search algorithm, the AI will then choose the best action from random simulation of the game with knowledge base for consideration. This best action will then be executed and the resulting score will be processed through SARSA algorithm, thus influencing the knowledge base. After the game has ended, the local knowledge base is tallied and saved to an external file for future use.

# How to Use
1. Copy the AzkAI.jar to "FightingICE/data/ai"
2. Create a new folder named "AzkAI" in "FightingICE/dataAi/"
3. Creat an empty text file named "data.txt" in the folder created before
4. Start the game

# Test Result
After 300 simulations, AzkAI will still fail to win against the first winner of 2015 FightingICE competition most of the time. This is because the opponent AI is hard-coded to exploit mechanics of the game. Another probable reason is that AzkAI is not considering combo attacks in the game, AzkAI will only consider immediate action and its consequences. 
