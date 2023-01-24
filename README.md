# coding-challenge
Coding challenges for various roles at The Org

Make sure to use a private github repository to develop your solution and share it with us either via a zip or by inviting us as collaborators. 

# Implementation notes: 

- Can't change the privacy of a fork so I will delete after meeting
 <img width="852" alt="image" src="https://user-images.githubusercontent.com/1588592/214258207-43742b28-6000-43b0-935c-207359e068d9.png">

- I have spent only a little time on analysing concurrency so I envision to talk that out on the assignment debrief. 
- Commit history is intentionally made so review is easier ;)
- Used Sangria and Cats-effect, besides Slick. 
- Couldn't reuse the tester proposed in the original code since I've used Sangria instead of Caliban. It added a bit of boilerplate and took me a big portion of what I estimated to spend on the challenge, but it's the technology I use the most and that I feel comfortable with. 
- Latest version of this branch has been uploaded to my personal dockerhub, so the app should work with the usual 
`docker-compose up --build`
