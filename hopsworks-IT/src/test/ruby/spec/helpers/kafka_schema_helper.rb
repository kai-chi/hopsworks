=begin
 This file is part of Hopsworks
 Copyright (C) 2019, Logical Clocks AB. All rights reserved

 Hopsworks is free software: you can redistribute it and/or modify it under the terms of
 the GNU Affero General Public License as published by the Free Software Foundation,
 either version 3 of the License, or (at your option) any later version.

 Hopsworks is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with this program.
 If not, see <https://www.gnu.org/licenses/>.
=end
module SchemaHelper

  def get_schema_by_id(project_id, schema_id)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/schemas/ids/#{schema_id}"
  end

  def get_subjects(project_id)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects"
  end

  def get_subject_versions(project_id, subject)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}/versions"
  end

  def delete_subject(project_id, subject)
    delete "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}"
  end

  def get_subject_details(project_id, subject, version)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}/versions/#{version}"
  end

  def get_subject_schema(project_id, subject, version)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}/versions/#{version}/schema"
  end

  def register_new_schema(project_id, subject, schema_content)
    post "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}/versions", {schema: "#{schema_content}"
  end

  def check_if_schema_registered(project_id, subject, schema_content)
    post "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}", {schema: "#{schema_content}"
  end

  def delete_subjects_version(project_id, subject, version)
    delete "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/subjects/#{subject}/versions/#{version}"
  end

  def check_compatibility(project_id, subject, version, schema_content)
    post "#{ENV['HOPSWORKS_API']}/project/#{project_id}/compatibility/subjects/#{subject}/versions/#{version}"
  end

  def update_project_config(project_id, new_compatibility)
    put "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/config", {compatibility:"#{new_compatibility}"}
  end

  def get_project_config(project_id)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/config"
  end

  def update_subject_config(project_id, subject, new_compatibility)
    put "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/config/#{subject}", {compatibility:"#{new_compatibility}"}
  end

  def get_subject_config(project_id, subject)
    get "#{ENV['HOPSWORKS_API']}/project/#{project_id}/kafka/config/#{subject}"
  end
end
