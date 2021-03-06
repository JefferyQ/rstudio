/*
 * FileLogWriter.hpp
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#ifndef FILE_LOG_WRITER_HPP
#define FILE_LOG_WRITER_HPP

#include <boost/thread.hpp>

#include <core/FilePath.hpp>
#include <core/LogOptions.hpp>
#include <core/LogWriter.hpp>

namespace rstudio {
namespace core {

class FileLogWriter : public LogWriter
{
public:
    FileLogWriter(const std::string& programIdentity,
                  int logLevel,
                  const FileLoggerOptions& options,
                  const std::string& logSection = std::string());
    virtual ~FileLogWriter();

    virtual void log(core::system::LogLevel level,
                     const std::string& message);
    virtual void log(const std::string& programIdentity,
                     core::system::LogLevel level,
                     const std::string& message);

    virtual int logLevel() { return logLevel_; }


private:
    void createFile();
    bool rotateLogFile();
    void rotateLogFilePrivileged();

    std::string programIdentity_;
    int logLevel_;
    FileLoggerOptions options_;
    FilePath logFile_;
    FilePath rotatedLogFile_;
    boost::mutex mutex_;
};

} // namespace core
} // namespace rstudio

#endif // FILE_LOG_WRITER_HPP
