FROM registry.applifier.info:5000/applifier-nodejs:5.2.0-1

ADD ./ /app
WORKDIR /app
RUN cd /app && npm install .

CMD ["/usr/bin/node","app.js"]
