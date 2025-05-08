Name: LinkedInMaxx

(Much more detailed in User Manual)

We created a Java-based web app that  models a simplified social network that users can 
use to become friends with people who share similar professional experiences and skills,
and are close in your network.

Users can signup by providing their resume and the people in the network they currently
are friends with. Afterwards, through our dashboard they can see the current LinkedInMaxx
social network. If they go to the Friend Distance card, they can see the minimum path and 
it's length between them and any other user in the network, calculated using BFS. 

If they go to Friend Recommendations card, they can see two people the system recommends 
them to become friends with based on our recommendation score, calculated by dividing
cosine similarity between documents made by your skills and experiences and the skills
and experiences of others, by the minimum distance between you and that user (more
detail in User Manual)

And then they can choose to befriend the recommended user which adds that edge to the graph.

We implemented the following categories:
Graph and graph algorithms
We use a friendship graph (via BFS) to compute “friend distance” and shortest paths.

Social Networks
Our app models users and friendships as a social network, and we use APIs to traverse and 
visualize that network.

Document Search
We tokenize profiles (experiences + skills), build inverted‐index statistics, and compute 
cosine similarity to “search” for similar users.

Work Breakdown:
Krishav:
Created the User Manual
Created the Signup Page UI and the SignupServlet/LoginServlet to parse the responses into a JSON file for the SQL database
Created the schema and seed data for the database
Wrote each of the DAO’s (Data Access Objects)

Anirudh:
Implemented the entire friend recommendation system, including RecommendServlet, TdidfService, and the recommendation page UI
Helped Sash with graph creation

Aarav:
Implemented most of the UI, including the dashboard pages, the distance page, the profile page, and index page
Wrote the BFS algorithm for finding shortest paths between you and another user (DistanceServlet)

Sash: 
Implemented the Resume Parser
Created the interactive graph for the dashboard and worked on adding new users and new connections to the graph
Helped integrate resume parser with the database
Helped with testing as well


Thank you!



