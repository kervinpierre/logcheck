package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.utils.LogCheckResult;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 7/27/2016.
 */
public interface ILogCheckTail extends Callable<LogCheckResult>
{
}
