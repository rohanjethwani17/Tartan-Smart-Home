The following is an example of our reversion pipeline functioning properly

1. the first time `docker compose images` is run, the image for `smart-home-platform-1` is `de3997f8a45d`
2. after merging a test PR, the image ID is now `694089ab890e`
3. after running the reversion workflow, the image ID has reverted to the previous `de3997f8a45d`

```
ubuntu@deployment:~/prod/smart-home$ docker compose images
CONTAINER                      REPOSITORY                   TAG                 IMAGE ID            SIZE
smart-home-house-cmu-1         smart-home-house-cmu         latest              cad78ecb6cd8        994MB
smart-home-house-mse-1         smart-home-house-mse         latest              f0b19f1f0647        994MB
smart-home-mysql-container-1   smart-home-mysql-container   active              b2886ad27552        783MB
smart-home-platform-1          smart-home-platform          active              de3997f8a45d        489MB
ubuntu@deployment:~/prod/smart-home$ # now merging PR to main - this will redeploy the backend
ubuntu@deployment:~/prod/smart-home$ docker compose images
CONTAINER                      REPOSITORY                   TAG                 IMAGE ID            SIZE
smart-home-house-cmu-1         smart-home-house-cmu         latest              cad78ecb6cd8        994MB
smart-home-house-mse-1         smart-home-house-mse         latest              f0b19f1f0647        994MB
smart-home-mysql-container-1   smart-home-mysql-container   active              b2886ad27552        783MB
smart-home-platform-1          smart-home-platform          active              694089ab890e        489MB
ubuntu@deployment:~/prod/smart-home$ # reversion workflow run
ubuntu@deployment:~/prod/smart-home$ docker compose images
CONTAINER                      REPOSITORY                   TAG                 IMAGE ID            SIZE
smart-home-house-cmu-1         smart-home-house-cmu         latest              cad78ecb6cd8        994MB
smart-home-house-mse-1         smart-home-house-mse         latest              f0b19f1f0647        994MB
smart-home-mysql-container-1   smart-home-mysql-container   active              b2886ad27552        783MB
smart-home-platform-1          smart-home-platform          active              de3997f8a45d        489MB
```