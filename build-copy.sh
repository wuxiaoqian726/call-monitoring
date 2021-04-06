#mvn -Prelease-all package
mvn clean
mvn -Prelease-client package

cd distribution/target
ls
rm -rf distribution.zip

zip -vr distribution.zip distribution-1.0.0/
scp distribution.zip root@121.40.53.80:/root

scp distribution.zip root@8.136.236.118:/root