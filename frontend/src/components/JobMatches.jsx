import React from 'react';
import { Briefcase, MapPin, Building, Check, X } from 'lucide-react';

export default function JobMatches({ jobs }) {
  if (!jobs || jobs.length === 0) {
    return (
      <div className="card jobs-card">
        <h3 className="card-title">Recommended Jobs</h3>
        <p className="empty-text">No jobs matched your profile.</p>
      </div>
    );
  }

  return (
    <div className="card jobs-card">
      <h3 className="card-title">Recommended Jobs</h3>
      <div className="notice">
        These are sample job listings used for demonstration and are not live vacancies.
      </div>
      
      <div className="jobs-list">
        {jobs.map((job, idx) => (
          <div key={idx} className="job-item">
            <div className="job-header">
              <h4 className="job-title">{job.title}</h4>
              <span className="job-score">{Math.round(job.match_percentage)}% Match</span>
            </div>
            
            <div className="job-meta">
              <span className="meta-item"><Building size={14}/> {job.company}</span>
              <span className="meta-item"><MapPin size={14}/> {job.location}</span>
              <span className="meta-item"><Briefcase size={14}/> {job.employment_type}</span>
            </div>

            <div className="job-skills-split">
              <div className="skills-column">
                <h5>Matched Skills</h5>
                <ul className="skill-list matched-skills">
                  {job.matched_skills && job.matched_skills.map((skill, i) => (
                    <li key={i}><Check size={14} className="icon-success"/> {skill}</li>
                  ))}
                  {(!job.matched_skills || job.matched_skills.length === 0) && (
                    <li className="empty-li">None</li>
                  )}
                </ul>
              </div>
              
              <div className="skills-column">
                <h5>Missing Skills</h5>
                <ul className="skill-list missing-skills">
                  {job.missing_skills && job.missing_skills.map((skill, i) => (
                    <li key={i}><X size={14} className="icon-error"/> {skill}</li>
                  ))}
                  {(!job.missing_skills || job.missing_skills.length === 0) && (
                    <li className="empty-li">None</li>
                  )}
                </ul>
              </div>
            </div>

            {job.description && <p className="job-description">{job.description}</p>}
          </div>
        ))}
      </div>
    </div>
  );
}
