FROM apache/hadoop:3
USER root

# Fix CentOS 7 mirrors, install Python3, which, and Java 8
RUN sed -i 's|mirrorlist=|#mirrorlist=|g' /etc/yum.repos.d/CentOS-*.repo && \
    sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-*.repo && \
    yum clean all && yum makecache && \
    yum install -y epel-release && \
    yum install -y python3 which java-1.8.0-openjdk-devel && \
    ln -sf /usr/bin/python3 /usr/local/bin/python3 && \
    yum clean all

ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

USER hadoop
