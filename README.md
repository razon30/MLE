# Dynamic Microservice Placement in Multi-Tier Fog Networks

## Overview
This repository contains the implementation code for our paper, *Dynamic Microservice Placement in Multi-Tier Fog Networks*, published in *Internet of Things*. The paper addresses the challenges of microservice placement in Industrial Internet-of-Things (IIoT) applications by proposing a tiered framework for dynamic microservice placement in Fog computing environments.

Our framework significantly improves resource utilization and responsiveness in IIoT applications by strategically placing microservices on Master and Citizen Fog devices.

## Paper Details
**Title:** Dynamic microservice placement in multi-tier Fog networks  
**Journal:** Internet of Things  
**Year:** 2024  
**DOI:** [10.1016/j.iot.2024.101224](https://doi.org/10.1016/j.iot.2024.101224)  
**Authors:** Md Razon Hossain, Md Whaiduzzaman, Alistair Barros, Colin Fidge  
**Keywords:** Microservice, Fog computing, IIoT, Seamless microservice execution, Resource provisioning  
**Link to Paper:** [ScienceDirect](https://www.sciencedirect.com/science/article/pii/S2542660524001653)

## Abstract
Fog computing extends Cloud-like infrastructure to the Edge, advancing IIoT applications by enabling deployment of business-oriented enterprise systems in proximity to sensor devices. This enhances responsiveness to IIoT events where timely decision-making is crucial. However, microservice placement in IIoT settings is challenging due to dynamic IIoT events and variable resource needs of microservices against resource-constrained, heterogeneous, and distributed Fog devices. 

The cost of matching and deploying microservices in real-time, dynamic settings can exceed the benefits of just-in-time Edge deployments. Current studies focus on managing microservice placement from Cloud servers and offloading to the Cloud when Fog devices lack resources. However, efficient Fog resource use and edge-based placement management are under-researched areas.

To address this, we develop a tiered framework for dynamic microservice placement. This framework dedicates costly resource decision-making for microservices to Master Fog devices, while Citizen Fog devices handle microservice execution. We implement a priority-based algorithm in each Master Fog to identify high-priority Edge-required microservices, sort Fog devices by resource availability, and place microservices based on priority and dependencies. Strategies for microservice placement, scaling, and request escalation are applied to manage a small number of Citizen Fog devices.

Our framework is evaluated in a simulated Fog environment and outperforms state-of-the-art frameworks in resource utilization, reducing Cloud dependency by one-third and average application execution time by 65-70%.

## Results
Our framework shows significant improvements in:

- Resource utilization
- Application execution time
- Cloud dependency
For detailed results, refer to our [paper](https://www.sciencedirect.com/science/article/pii/S2542660524001653).

## Citation
If you use this code in your research, please cite our paper:

```
@article{HOSSAIN2024101224,
title = {Dynamic microservice placement in multi-tier Fog networks},
journal = {Internet of Things},
pages = {101224},
year = {2024},
issn = {2542-6605},
doi = {https://doi.org/10.1016/j.iot.2024.101224},
url = {https://www.sciencedirect.com/science/article/pii/S2542660524001653},
author = {Md Razon Hossain and Md Whaiduzzaman and Alistair Barros and Colin Fidge},
keywords = {Microservice, Fog computing, IIoT, Seamless microservice execution, Resource provisioning}
}
```






