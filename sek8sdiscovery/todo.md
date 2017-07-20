1.) extract namespace from openshift OPENSHIFT_BUILD_NAMESPACE ENV
2.) echeck if OpenShift (OPENSHIFT_BUILD_NAME or OPENSHIFT_BUILD_NAMESPACE)

Users:  developer
        system:admin
        system:serviceaccount:default:developer
        system:serviceaccount:default:pvinstaller
        system:serviceaccount:myproject:deployer
        system:serviceaccount:openshift-infra:build-controller
        system:serviceaccount:openshift-infra:daemonset-controller
        system:serviceaccount:openshift-infra:deploymentconfig-controller
        system:serviceaccount:openshift-infra:job-controller
        system:serviceaccount:openshift-infra:pv-binder-controller
        system:serviceaccount:openshift-infra:pv-recycler-controller
        system:serviceaccount:openshift-infra:replicaset-controller
        system:serviceaccount:openshift-infra:replication-controller
        system:serviceaccount:openshift-infra:statefulset-controller
        
        https://gist.github.com/cmoulliard/8187710903ba975c9477c9438e06a5cc
        https://github.com/fabric8io/jenkins-docker/issues/114
        
        
        
   Solution when using API key:     oadm policy add-role-to-user view system:serviceaccount:myproject:default -n myproject