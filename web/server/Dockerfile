# Specify Node base image 
FROM node:8

ENV APP=/app

# Copy source code
COPY . $APP

# Change working directory
WORKDIR $APP

# Set a non-root location for any global dependencies
ENV NPM_CONFIG_PREFIX=/home/node/.npm-global

# Install dependencies
RUN npm install

# Expose server port to the outside
EXPOSE 3000

# Run as user "node"
USER node

COPY ./docker-entrypoint.sh /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

CMD ["bin/www"]