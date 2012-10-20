package com.example.mobile_qmon;

interface QueueInterface {
	String retrieveJobs();
	String retrieveErrorMessage();
	int retrieveErrorStatus();
}