FROM openjdk:8-jre

# Set the URL to download
ARG PRESTO_BIN=https://repo1.maven.org/maven2/com/facebook/presto/presto-server/0.270/presto-server-0.270.tar.gz

# Update the base image OS and install wget and python
RUN apt-get update
RUN apt-get install -y wget python less

# Download Presto and unpack it to /opt/presto
RUN wget --quiet ${PRESTO_BIN}
RUN mkdir -p /opt
RUN tar -xf presto-server-0.270.tar.gz -C /opt
RUN rm presto-server-0.270.tar.gz
RUN ln -s /opt/presto-server-0.270 /opt/presto

# Copy configuration files on the host into the image
COPY etc /opt/presto/etc

# Download the Presto CLI and put it in the image
RUN wget --quiet https://repo1.maven.org/maven2/com/facebook/presto/presto-cli/0.270/presto-cli-0.270-executable.jar
RUN mv presto-cli-0.270-executable.jar /usr/local/bin/presto
RUN chmod +x /usr/local/bin/presto

# Specify the entrypoint to start
ENTRYPOINT /opt/presto/bin/launcher run